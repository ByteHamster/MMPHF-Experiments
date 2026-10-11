/*
 * SPDX-FileCopyrightText: 2025 Sebastiano Vigna
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

mod benchmark_data;
mod contender;
mod lcp2_mmphf_int_contender;
mod lcp2_mmphf_str_contender;
mod lcp_mmphf_int_contender;
mod lcp_mmphf_str_contender;

use anyhow::{Result, bail};
use benchmark_data::{load_int32_file, load_int64_file, load_string_file};
use clap::Parser;
use contender::run;
use lcp_mmphf_int_contender::LcpMmphfIntContender;
use lcp_mmphf_str_contender::LcpMmphfStrContender;
use lcp2_mmphf_int_contender::Lcp2MmphfIntContender;
use lcp2_mmphf_str_contender::Lcp2MmphfStrContender;
use std::path::PathBuf;

#[derive(Parser, Debug)]
#[command(
    about = "Rust MMPHF benchmark (sux-rs LcpMmphf, Lcp2Mmphf).",
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

fn main() -> Result<()> {
    env_logger::builder()
        .filter_level(log::LevelFilter::Info)
        .try_init()?;

    let args = Args::parse();
    let max_n = args.max_n.unwrap_or(usize::MAX);

    let dataset = args
        .filename
        .file_name()
        .unwrap()
        .to_string_lossy()
        .to_string();

    match args.data_type.as_str() {
        "strings" => {
            let keys = load_string_file(&args.filename, max_n)?;
            if keys.len() < 2 {
                eprintln!("Input file does not contain strings");
                return Ok(());
            }
            run::<String, LcpMmphfStrContender>(&dataset, &keys, args.num_queries)?;
            run::<String, Lcp2MmphfStrContender>(&dataset, &keys, args.num_queries)?;
        }
        "int64" => {
            let keys = load_int64_file(&args.filename, max_n)?;
            if keys.len() < 2 {
                eprintln!("Input file does not contain integers");
                return Ok(());
            }
            run::<u64, LcpMmphfIntContender<u64>>(&dataset, &keys, args.num_queries)?;
            run::<u64, Lcp2MmphfIntContender<u64>>(&dataset, &keys, args.num_queries)?;
        }
        "int32" => {
            let keys = load_int32_file(&args.filename, max_n)?;
            if keys.len() < 2 {
                eprintln!("Input file does not contain integers");
                return Ok(());
            }
            run::<u32, LcpMmphfIntContender<u32>>(&dataset, &keys, args.num_queries)?;
            run::<u32, Lcp2MmphfIntContender<u32>>(&dataset, &keys, args.num_queries)?;
        }
        other => {
            bail!("Unknown input type: {other}");
        }
    }

    Ok(())
}
