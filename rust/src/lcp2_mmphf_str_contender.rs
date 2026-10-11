use crate::contender::Contender;
use anyhow::Result;
use dsi_progress_logger::ProgressLogger;
use mem_dbg::{MemSize, SizeFlags};
use sux::func::{Lcp2MmphfStr, VBuilder};
use sux::traits::{TryIntoUnaligned, Unaligned};
use sux::utils::FromSlice;

pub struct Lcp2MmphfStrContender {
    mmphf: Unaligned<Lcp2MmphfStr>,
}

impl Contender<String> for Lcp2MmphfStrContender {
    fn name() -> &'static str {
        "Lcp2MmphfRust"
    }

    fn construct(keys: &[String]) -> Result<Self> {
        let mut pl = ProgressLogger::default();
        let mmphf = <Lcp2MmphfStr>::try_new_with_builder(
            FromSlice::new(keys),
            keys.len(),
            VBuilder::default().max_num_threads(1),
            &mut pl,
        )?;
        Ok(Self {
            mmphf: mmphf.try_into_unaligned()?,
        })
    }

    fn size_bits(&self) -> usize {
        self.mmphf.mem_size(SizeFlags::default()) * 8
    }

    #[inline(always)]
    fn query(&self, key: &String) -> usize {
        self.mmphf.get(key)
    }
}
