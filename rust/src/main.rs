/*
 * SPDX-FileCopyrightText: 2025 Sebastiano Vigna
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

//! Rust MMPHF benchmark binary for the MMPHF-Experiments framework.
//!
//! Accepts the same CLI interface as the C++ and Java binaries and outputs
//! `RESULT` lines in the same format expected by sqlplot-tools.

use anyhow::{Context, Result, bail};
use clap::Parser;
use dsi_progress_logger::ProgressLogger;
use mem_dbg::{MemSize, SizeFlags};
use std::hint::black_box;
use std::path::{Path, PathBuf};
use std::time::Instant;
use sux::func::hollow_trie::{HtDistMmphfInt, HtDistMmphfStr};
use sux::func::{Lcp2MmphfInt, Lcp2MmphfStr, LcpMmphfInt, LcpMmphfStr};
use sux::traits::{TryIntoUnaligned, Unaligned};
use sux::utils::FromSlice;

#[derive(Parser, Debug)]
#[command(
    about = "Rust MMPHF benchmark (sux-rs LcpMmphf, Lcp2Mmphf, HtDistMmphf).",
    long_about = None,
    next_line_help = true,
    max_term_width = 100
)]
struct Args {
    /// Input data set type: strings, int64, or int32.
    #[arg(short = 't', long = "type", default_value = "strings")]
    data_type: String,

    /// Input data set file path.
    #[arg(short = 'f', long)]
    filename: PathBuf,

    /// Number of queries to perform.
    #[arg(short = 'q', long = "numQueries", default_value = "100")]
    num_queries: usize,

    /// Truncate input file to this many keys.
    #[arg(short = 'n', long = "maxN")]
    max_n: Option<usize>,
}

// ── File I/O (matching C++ BenchmarkData.h formats) ──────────────────

/// Read a SOSD-format binary file: little-endian u64 count, then N
/// little-endian u64 values.
fn load_int64_file(path: &Path, max_n: usize) -> Result<Vec<u64>> {
    println!("Loading input file");
    let data = std::fs::read(path).with_context(|| format!("reading {}", path.display()))?;
    if data.len() < 8 {
        bail!("File too small for SOSD header");
    }
    let n = u64::from_le_bytes(data[0..8].try_into().unwrap()) as usize;
    let n = n.min(max_n);
    println!("Loading input file of size {n}");
    let expected = 8 + n * 8;
    if data.len() < expected {
        bail!(
            "File size {} < expected {} (header says {n} keys)",
            data.len(),
            expected
        );
    }
    let mut keys = vec![0u64; n];
    for (i, chunk) in data[8..8 + n * 8].chunks_exact(8).enumerate() {
        keys[i] = u64::from_le_bytes(chunk.try_into().unwrap());
    }
    println!("Checking if input data is sorted");
    for i in 1..keys.len() {
        if keys[i] < keys[i - 1] {
            bail!("Not sorted or duplicate key");
        }
    }
    println!("Loaded {n} integers");
    Ok(keys)
}

/// Read a SOSD-format int32 binary file: little-endian u32 count, then
/// N little-endian u32 values.
fn load_int32_file(path: &Path, max_n: usize) -> Result<Vec<u32>> {
    println!("Loading input file");
    let data = std::fs::read(path).with_context(|| format!("reading {}", path.display()))?;
    if data.len() < 4 {
        bail!("File too small for int32 header");
    }
    let n = u32::from_le_bytes(data[0..4].try_into().unwrap()) as usize;
    let n = n.min(max_n);
    println!("Loading input file of size {n}");
    let expected = 4 + n * 4;
    if data.len() < expected {
        bail!(
            "File size {} < expected {} (header says {n} keys)",
            data.len(),
            expected
        );
    }
    let mut keys = Vec::with_capacity(n);
    for chunk in data[4..4 + n * 4].chunks_exact(4) {
        keys.push(u32::from_le_bytes(chunk.try_into().unwrap()));
    }
    println!("Loaded {n} integers");
    for i in 1..keys.len() {
        if keys[i] <= keys[i - 1] {
            bail!("Not sorted or duplicate key");
        }
    }
    Ok(keys)
}

/// Read a newline-separated sorted string file into a contiguous byte
/// buffer.
fn load_string_file(path: &Path) -> Result<Vec<u8>> {
    println!("Loading input file");
    std::fs::read(path).with_context(|| format!("reading {}", path.display()))
}

/// Build string slices from a newline-separated contiguous buffer,
/// returning at most `max_n` keys.
fn string_keys(data: &[u8], max_n: usize) -> Result<Vec<&str>> {
    let text = std::str::from_utf8(data).context("input is not valid UTF-8")?;
    let mut keys: Vec<&str> = Vec::new();
    for line in text.lines() {
        if line.is_empty() {
            continue;
        }
        keys.push(line);
        if keys.len() >= max_n {
            break;
        }
    }
    println!("Loaded {} strings", keys.len());
    Ok(keys)
}

// ── Benchmark runners ────────────────────────────────────────────────

/// Benchmark LcpMmphfInt on u64 keys. Outputs RESULT lines matching
/// the C++/Java format.
fn bench_int64(dataset: &str, keys: &[u64], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: LcpMmphfRust");

    // Cooldown (matching C++/Java methodology)
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<LcpMmphfInt<u64>> =
        LcpMmphfInt::try_par_new(keys, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness (first 100k, matching C++/Java)
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut query_plan: Vec<u64> = Vec::with_capacity(num_queries);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            query_plan.push(keys[idx]);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for &key in &query_plan {
            black_box(func.get(key));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=LcpMmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

/// Benchmark LcpMmphfStr on string keys.
fn bench_strings(dataset: &str, keys: &[&str], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: LcpMmphfRust");

    // Cooldown
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<LcpMmphfStr> =
        LcpMmphfStr::try_par_new(keys, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query — pack query strings contiguously
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut packed_data: Vec<u8> = Vec::new();
        let mut packed_offsets: Vec<u32> = Vec::with_capacity(num_queries + 1);
        packed_offsets.push(0);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            packed_data.extend_from_slice(keys[idx].as_bytes());
            packed_offsets.push(packed_data.len() as u32);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for i in 0..num_queries {
            let s = packed_offsets[i] as usize;
            let e = packed_offsets[i + 1] as usize;
            // SAFETY: packed_data was built from valid UTF-8 strings.
            let q = unsafe { std::str::from_utf8_unchecked(&packed_data[s..e]) };
            black_box(func.get(q));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=LcpMmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

/// Benchmark Lcp2MmphfInt on u64 keys.
fn bench_int64_lcp2(dataset: &str, keys: &[u64], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: Lcp2MmphfRust");

    // Cooldown (matching C++/Java methodology)
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<Lcp2MmphfInt<u64>> =
        Lcp2MmphfInt::try_par_new(keys, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness (first 100k, matching C++/Java)
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut query_plan: Vec<u64> = Vec::with_capacity(num_queries);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            query_plan.push(keys[idx]);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for &key in &query_plan {
            black_box(func.get(key));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=Lcp2MmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

/// Benchmark LcpMmphfInt on u32 keys.
fn bench_int32(dataset: &str, keys: &[u32], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: LcpMmphfRust");

    // Cooldown (matching C++/Java methodology)
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<LcpMmphfInt<u32>> =
        LcpMmphfInt::try_par_new(keys, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness (first 100k, matching C++/Java)
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut query_plan: Vec<u32> = Vec::with_capacity(num_queries);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            query_plan.push(keys[idx]);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for &key in &query_plan {
            black_box(func.get(key));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=LcpMmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

/// Benchmark Lcp2MmphfInt on u32 keys.
fn bench_int32_lcp2(dataset: &str, keys: &[u32], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: Lcp2MmphfRust");

    // Cooldown (matching C++/Java methodology)
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<Lcp2MmphfInt<u32>> =
        Lcp2MmphfInt::try_par_new(keys, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness (first 100k, matching C++/Java)
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut query_plan: Vec<u32> = Vec::with_capacity(num_queries);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            query_plan.push(keys[idx]);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for &key in &query_plan {
            black_box(func.get(key));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=Lcp2MmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

/// Benchmark Lcp2MmphfStr on string keys.
fn bench_strings_lcp2(dataset: &str, keys: &[&str], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: Lcp2MmphfRust");

    // Cooldown
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<Lcp2MmphfStr> =
        Lcp2MmphfStr::try_par_new(keys, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query — pack query strings contiguously
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut packed_data: Vec<u8> = Vec::new();
        let mut packed_offsets: Vec<u32> = Vec::with_capacity(num_queries + 1);
        packed_offsets.push(0);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            packed_data.extend_from_slice(keys[idx].as_bytes());
            packed_offsets.push(packed_data.len() as u32);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for i in 0..num_queries {
            let s = packed_offsets[i] as usize;
            let e = packed_offsets[i + 1] as usize;
            // SAFETY: packed_data was built from valid UTF-8 strings.
            let q = unsafe { std::str::from_utf8_unchecked(&packed_data[s..e]) };
            black_box(func.get(q));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=Lcp2MmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

/// Benchmark HtDistMmphfInt on u64 keys.
fn bench_int64_ht_dist(dataset: &str, keys: &[u64], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: HtDistMmphfRust");

    // Cooldown (matching C++/Java methodology)
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<HtDistMmphfInt<u64>> =
        HtDistMmphfInt::try_new(FromSlice::new(keys), n, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness (first 100k, matching C++/Java)
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut query_plan: Vec<u64> = Vec::with_capacity(num_queries);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            query_plan.push(keys[idx]);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for &key in &query_plan {
            black_box(func.get(key));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=HtDistMmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

/// Benchmark HtDistMmphfStr on string keys.
fn bench_strings_ht_dist(dataset: &str, keys: &[&str], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: HtDistMmphfRust");

    // Cooldown
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<HtDistMmphfStr> =
        HtDistMmphfStr::try_new(FromSlice::new(keys), n, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query — pack query strings contiguously
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut packed_data: Vec<u8> = Vec::new();
        let mut packed_offsets: Vec<u32> = Vec::with_capacity(num_queries + 1);
        packed_offsets.push(0);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            packed_data.extend_from_slice(keys[idx].as_bytes());
            packed_offsets.push(packed_data.len() as u32);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for i in 0..num_queries {
            let s = packed_offsets[i] as usize;
            let e = packed_offsets[i + 1] as usize;
            // SAFETY: packed_data was built from valid UTF-8 strings.
            let q = unsafe { std::str::from_utf8_unchecked(&packed_data[s..e]) };
            black_box(func.get(q));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=HtDistMmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

/// Benchmark HtDistMmphfInt on u32 keys.
fn bench_int32_ht_dist(dataset: &str, keys: &[u32], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: HtDistMmphfRust");

    // Cooldown (matching C++/Java methodology)
    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    // Construction
    println!("Constructing");
    let start = Instant::now();
    let mut pl = ProgressLogger::default();
    let func: Unaligned<HtDistMmphfInt<u32>> =
        HtDistMmphfInt::try_new(FromSlice::new(keys), n, &mut pl)?.try_into_unaligned()?;
    let construction_ms = start.elapsed().as_millis() as u64;

    // Test correctness (first 100k, matching C++/Java)
    println!("Testing");
    let check_n = n.min(100_000);
    for (i, &key) in keys[..check_n].iter().enumerate() {
        let got = func.get(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    // Space
    let bits_per_element = func.mem_size(SizeFlags::default()) as f64 * 8.0 / n as f64;

    // Query
    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let mut query_plan: Vec<u32> = Vec::with_capacity(num_queries);
        for _ in 0..num_queries {
            let idx = rand::random_range(0..n);
            query_plan.push(keys[idx]);
        }

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for &key in &query_plan {
            black_box(func.get(key));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    println!(
        "RESULT dataset={dataset} name=HtDistMmphfRust \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}"
    );

    Ok(())
}

// ── Main ────────────────────────────────────────────────────────────

fn main() -> Result<()> {
    env_logger::builder()
        .filter_level(log::LevelFilter::Info)
        .try_init()?;

    let args = Args::parse();
    let max_n = args.max_n.unwrap_or(usize::MAX);

    // Extract dataset name from filename (matching C++/Java behavior)
    let dataset = args
        .filename
        .file_name()
        .unwrap()
        .to_string_lossy()
        .to_string();

    match args.data_type.as_str() {
        "strings" => {
            let data = load_string_file(&args.filename)?;
            let keys = string_keys(&data, max_n)?;
            if keys.len() < 2 {
                eprintln!("Input file does not contain strings");
                return Ok(());
            }
            bench_strings(&dataset, &keys, args.num_queries)?;
            bench_strings_lcp2(&dataset, &keys, args.num_queries)?;
            bench_strings_ht_dist(&dataset, &keys, args.num_queries)?;
        }
        "int64" => {
            let keys = load_int64_file(&args.filename, max_n)?;
            if keys.len() < 2 {
                eprintln!("Input file does not contain integers");
                return Ok(());
            }
            bench_int64(&dataset, &keys, args.num_queries)?;
            bench_int64_lcp2(&dataset, &keys, args.num_queries)?;
            bench_int64_ht_dist(&dataset, &keys, args.num_queries)?;
        }
        "int32" => {
            let keys = load_int32_file(&args.filename, max_n)?;
            if keys.len() < 2 {
                eprintln!("Input file does not contain integers");
                return Ok(());
            }
            bench_int32(&dataset, &keys, args.num_queries)?;
            bench_int32_lcp2(&dataset, &keys, args.num_queries)?;
            bench_int32_ht_dist(&dataset, &keys, args.num_queries)?;
        }
        other => {
            bail!("Unknown input type: {other}");
        }
    }

    Ok(())
}
