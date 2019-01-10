using System;
using System.Collections.Generic;
using System.IO.MemoryMappedFiles;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace TaskParallelLibrary
{
    public class MapSelectedItems
    {
        public static List<TR> SequentialMapSelectedItems<T, TR>(IEnumerable<T> items, Predicate<T> selector,
            Func<T, TR> mapper, CancellationToken cToken)
        {
            var result = new List<TR>();
            foreach (T item in items)
            {
                cToken.ThrowIfCancellationRequested();
                if (selector(item)) result.Add(mapper(item));
            }

            return result;
        }

        public static List<TR> ParallelMapSelectedItems<T, TR>(IEnumerable<T> items, Predicate<T> selector,
            Func<T, TR> mapper, CancellationToken cToken)
        {
            List<TR> result = new List<TR>();

            object monitor = new object();

            Parallel.ForEach(
                items,
                () => new List<TR>(),
                (item, state, local) =>
                {
                    cToken.ThrowIfCancellationRequested();
                    if (selector(item))
                    {
                        local.Add(mapper(item));
                    }

                    return local;
                },
                (toAccumulate) =>
                {
                    cToken.ThrowIfCancellationRequested();

                    lock (monitor)
                    {
                        result = result.Concat(toAccumulate).ToList();
                    }
                });

            return result;
        }
    }
}