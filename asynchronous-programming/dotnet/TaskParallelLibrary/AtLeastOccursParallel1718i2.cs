using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace TaskParallelLibrary
{
    public class AtLeastOccursParallel1718i2
    {
        public static bool AtLeastOccursParallel<T>(IEnumerable<T> items, Predicate<T> selector, int occurrences,
            CancellationToken ctoken)
        {
            int actualOccurrences = 0;

            ParallelOptions options = new ParallelOptions {CancellationToken = ctoken};

            object monitor = new object();

            Parallel.ForEach(
                items,
                options,
                () => 0,
                (item, state, local) =>
                {
                    options.CancellationToken.ThrowIfCancellationRequested();

                    if (!selector(item)) return local;

                    local++;

                    if (local >= occurrences)
                    {
                        state.Stop();
                    }

                    return local;
                },
                (toAccumulate) =>
                {
                    lock (monitor)
                    {
                        if (actualOccurrences < occurrences)
                        {
                            actualOccurrences += toAccumulate;
                        }
                    }
                });

            return actualOccurrences >= occurrences;
        }
    }
}