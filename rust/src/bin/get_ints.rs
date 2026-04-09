/*
 * SPDX-FileCopyrightText: 2025 Sebastiano Vigna
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

use anyhow::Result;
use clap::{Parser, ValueEnum};
use rand::RngExt;
use std::io::{BufWriter, Write};
use std::path::PathBuf;

#[derive(Debug, Clone, ValueEnum)]
enum Dist {
    Uniform,
    Geometric,
}

#[derive(Parser, Debug)]
#[command(
    about = "Generates random integers with nonuniform distances.",
    long_about = None,
    next_line_help = true,
    max_term_width = 100
)]
struct Args {
    /// The number of int64 elements to generate.
    n: usize,

    /// The distribution of the distances between consecutive values.
    #[arg(value_enum)]
    dist: Dist,

    /// Output file path.
    filename: PathBuf,
}

/// Generates n random int64 values by extracting their distances
/// from a given distribution. The distribution is tuned so
/// that its expected sum is slightly less than 2^64.
pub fn main() -> Result<()> {
    let args = Args::parse();
    let n = args.n;

    let mut rng = rand::rng();
    let mut values: Vec<u64> = Vec::with_capacity(n);

    loop {
        values.clear();
        let mut current: u64 = 0;
        let mut overflowed = false;

        match args.dist {
            Dist::Uniform => {
                // Uniform in [1, ⌊2^65/n⌋]; mean ≈ 2^64/n.
                let max_gap =
                    (2 * (u64::MAX as u128) / (n as u128)).min(u64::MAX as u128) as u64;
                for _ in 0..n {
                    let gap: u64 = rng.random_range(1..=max_gap);
                    match current.checked_add(gap) {
                        Some(v) => {
                            current = v;
                            values.push(current);
                        }
                        None => {
                            overflowed = true;
                            break;
                        }
                    }
                }
            }
            Dist::Geometric => {
                // Geometric with p = n/2^64, shifted to {1, 2, …}; mean = 1/p ≈ 2^64/n.
                let p = n as f64 / u64::MAX as f64;
                let inv_ln_1_minus_p = 1.0 / (-p).ln_1p();
                for _ in 0..n {
                    let u: f64 = rng.random_range(f64::MIN_POSITIVE..1.0);
                    let gap = (u.ln() * inv_ln_1_minus_p) as u64 + 1;
                    match current.checked_add(gap) {
                        Some(v) => {
                            current = v;
                            values.push(current);
                        }
                        None => {
                            overflowed = true;
                            break;
                        }
                    }
                }
            }
        }

        if !overflowed {
            break;
        }
        eprintln!("Overflow after {} values, retrying...", values.len());
    }

    let file = std::fs::File::create(&args.filename)?;
    let mut writer = BufWriter::new(file);
    writer.write_all(&(n as u64).to_le_bytes())?;
    for &v in &values {
        writer.write_all(&v.to_le_bytes())?;
    }

    Ok(())
}
