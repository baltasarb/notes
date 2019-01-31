using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace TaskParallelLibrary
{
    public class ParallelMapSelected
    {
        public static List<O> ParallelMapSelectedImp<I, O>(IEnumerable<I> items, Predicate<I> selector,
            Func<I, O> mapper,
            CancellationToken cancellationToken)
        {
            List<O> results = new List<O>();
            var monitor = new object();

            ParallelOptions options = new ParallelOptions {CancellationToken = cancellationToken};

            Parallel.ForEach(
                items,
                options,
                () => new List<O>(),
                (item, state, local) =>
                {
                    cancellationToken.ThrowIfCancellationRequested();

                    if (!selector(item)) return local;

                    var mappedItem = mapper(item);
                    local.Add(mappedItem);

                    return local;
                },
                localFinally: (toAccumulate) =>
                {
                    cancellationToken.ThrowIfCancellationRequested();

                    lock (monitor)
                    {
                        results.AddRange(toAccumulate);
                    }
                });
            return results;
        }
    }
}