use crate::contender::Contender;
use anyhow::Result;
use dsi_progress_logger::ProgressLogger;
use mem_dbg::{MemSize, SizeFlags};
use sux::func::{LcpMmphfStr, VBuilder};
use sux::traits::{TryIntoUnaligned, Unaligned};
use sux::utils::FromSlice;

pub struct LcpMmphfStrContender {
    mmphf: Unaligned<LcpMmphfStr>,
}

impl Contender<String> for LcpMmphfStrContender {
    fn name() -> &'static str {
        "LcpMmphfRust"
    }

    fn construct(keys: &[String]) -> Result<Self> {
        let mut pl = ProgressLogger::default();
        let mmphf = <LcpMmphfStr>::try_new_with_builder(
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
