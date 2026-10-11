use anyhow::{Context, Result, bail};
use std::path::Path;

pub fn load_int64_file(path: &Path, max_n: usize) -> Result<Vec<u64>> {
    println!("Loading input file");
    let data = std::fs::read(path).with_context(|| format!("reading {}", path.display()))?;
    let (header, body) = data.split_at_checked(8).context("File too small for header")?;
    let n = (u64::from_le_bytes(header.try_into().unwrap()) as usize).min(max_n);
    println!("Loading input file of size {n}");
    let keys: Vec<u64> = body
        .chunks_exact(8)
        .take(n)
        .map(|chunk| u64::from_le_bytes(chunk.try_into().unwrap()))
        .collect();
    if keys.len() < n {
        bail!("File contains fewer keys than its header says");
    }
    println!("Checking if input data is sorted");
    if keys.windows(2).any(|w| w[1] < w[0]) {
        bail!("Not sorted or duplicate key");
    }
    println!("Loaded {n} integers");
    Ok(keys)
}

pub fn load_int32_file(path: &Path, max_n: usize) -> Result<Vec<u32>> {
    println!("Loading input file");
    let data = std::fs::read(path).with_context(|| format!("reading {}", path.display()))?;
    let (header, body) = data.split_at_checked(4).context("File too small for header")?;
    let n = (u32::from_le_bytes(header.try_into().unwrap()) as usize).min(max_n);
    println!("Loading input file of size {n}");
    let keys: Vec<u32> = body
        .chunks_exact(4)
        .take(n)
        .map(|chunk| u32::from_le_bytes(chunk.try_into().unwrap()))
        .collect();
    if keys.len() < n {
        bail!("File contains fewer keys than its header says");
    }
    println!("Loaded {n} integers");
    if keys.windows(2).any(|w| w[1] <= w[0]) {
        bail!("Not sorted or duplicate key");
    }
    Ok(keys)
}

pub fn load_string_file(path: &Path, max_n: usize) -> Result<Vec<String>> {
    println!("Loading input file");
    let text =
        std::fs::read_to_string(path).with_context(|| format!("reading {}", path.display()))?;
    let keys: Vec<String> = text
        .lines()
        .filter(|line| !line.is_empty())
        .take(max_n)
        .map(str::to_owned)
        .collect();
    println!("Loaded {} strings", keys.len());
    Ok(keys)
}
