use anyhow::{Result, bail};
use std::hint::black_box;
use std::time::Instant;

pub trait Contender<T: Clone>: Sized {
    fn name() -> &'static str;
    fn construct(keys: &[T]) -> Result<Self>;
    fn size_bits(&self) -> usize;
    fn query(&self, key: &T) -> usize;
}

pub fn run<T: Clone, C: Contender<T>>(dataset: &str, keys: &[T], num_queries: usize) -> Result<()> {
    let n = keys.len();

    println!("\nContender: {}", C::name());

    println!("Cooldown");
    std::thread::sleep(std::time::Duration::from_secs(3));

    println!("Constructing");
    let start = Instant::now();
    let mmphf = C::construct(keys)?;
    let construction_ms = start.elapsed().as_millis() as u64;

    println!("Testing");
    for (i, key) in keys.iter().take(100_000).enumerate() {
        let got = mmphf.query(key);
        if got != i {
            bail!("Error: Key at index {i} is not monotone minimal perfect (output: {got})");
        }
    }

    let mut query_ms: u64 = 0;
    if num_queries > 0 {
        println!("Preparing query plan");
        let query_plan: Vec<T> = (0..num_queries)
            .map(|_| keys[rand::random_range(0..n)].clone())
            .collect();

        println!("Cooldown");
        std::thread::sleep(std::time::Duration::from_secs(3));

        println!("Querying");
        let start = Instant::now();
        for key in &query_plan {
            black_box(mmphf.query(key));
        }
        query_ms = start.elapsed().as_millis() as u64;
    }

    let bits_per_element = mmphf.size_bits() as f64 / n as f64;
    println!(
        "RESULT dataset={dataset} \
         name={} \
         bitsPerElement={bits_per_element} \
         constructionTimeMilliseconds={construction_ms} \
         queryTimeMilliseconds={query_ms} \
         numQueries={num_queries} \
         N={n}",
        C::name()
    );

    Ok(())
}
