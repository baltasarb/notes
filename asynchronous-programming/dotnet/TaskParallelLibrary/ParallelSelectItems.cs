using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace TaskParallelLibrary
{
    public class ParallelSelectItems
    {
        static List<T> SequentialSelect<T>(IEnumerable<T> items, IEnumerable<T> keys, CancellationToken ct)
        {
            List<T> result = new List<T>();

            foreach (T item in items)
            {
                ct.ThrowIfCancellationRequested();
                foreach (T key in keys)
                    if (item.Equals(key))
                    {
                        result.Add(item);
                        break;
                    }
            }

            return result;
        }

        static List<T> ParallelPLinkSelect<T>(IEnumerable<T> items, IEnumerable<T> keys, CancellationToken ct)
        {
            return items
                .AsParallel()
                .Where(keys.Contains)
                .WithCancellation(ct)
                .ToList();
        }

        static List<T> ParallelForeachSelect<T>(IEnumerable<T> items, IEnumerable<T> keys, CancellationToken ct)
        {
            List<T> result = new List<T>();

            Parallel.ForEach(
                items,
                () => new LinkedList<T>(),
                (item, loopState, localList) =>
                {
                    ct.ThrowIfCancellationRequested();

                    if (keys.Contains(item))
                    {
                        localList.AddFirst(item);
                    }

                    return localList;
                }, list =>
                {
                    //to avoid lock usage in every iteration, an aggregator is used, using lock only on result joining
                    lock (result)
                    {
                        result.Add(list.First.Value);
                    }
                });

            return result;
        }
    }
}