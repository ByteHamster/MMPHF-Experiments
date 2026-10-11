use crate::contender::Contender;
use anyhow::Result;
use dsi_progress_logger::ProgressLogger;
use mem_dbg::{MemSize, SizeFlags};
use num_primitive::PrimitiveInteger;
use std::fmt::Debug;
use sux::func::{IntBitPrefix, LcpMmphfInt, VBuilder};
use sux::traits::{TryIntoUnaligned, Unaligned};
use sux::utils::{FromSlice, ToSig};

pub struct LcpMmphfIntContender<T> {
    mmphf: Unaligned<LcpMmphfInt<T>>,
}

impl<T> Contender<T> for LcpMmphfIntContender<T>
where
    T: PrimitiveInteger + ToSig<[u64; 2]> + Debug + Send + Sync + Copy + Ord,
    IntBitPrefix<T>: ToSig<[u64; 1]>,
{
    fn name() -> &'static str {
        "LcpMmphfRust"
    }

    fn construct(keys: &[T]) -> Result<Self> {
        let mut pl = ProgressLogger::default();
        let mmphf = <LcpMmphfInt<T>>::try_new_with_builder(
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
    fn query(&self, key: &T) -> usize {
        self.mmphf.get(*key)
    }
}
