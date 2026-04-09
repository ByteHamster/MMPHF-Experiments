/*
 * SPDX-FileCopyrightText: 2025 Sebastiano Vigna
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

use clap::Parser;

#[derive(Debug, Clone)]
enum Dist {
    Uniform,
    Geometric,
    Zipf,
}

#[derive(Parser, Debug)]
#[command(
     about = "Generates random integer with nonuniform distances.",
     long_about = None,
     next_line_help = true,
     max_term_width = 100
 )]
struct Args {
    /// The number of int64 element to generate.
    n: usize,

    /// Input data set file path.
    dist: Dist,
}

/// Generates n random int64 element by extracting their distances
/// from a given distribution. The distribution is tuned ⊆
/// that its average is 2^64 / n.

pub fn main() {
    let args = Args::parse();

    let n = args.n;
    let dist = args.dist;
}
