using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace TaskParallelLibrary
{
    public class ParallelSelect1718v
    {
        //given non parallel version
        static List<T> SequentialSelect<T>(IEnumerable<T> items, IEnumerable<T> keys, int count, CancellationToken ct)
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

                if (result.Count >= count) break;
            }

            return result;
        }

        static List<T> ParallelSelect<T>(IEnumerable<T> items, IEnumerable<T> keys, int count, CancellationToken ct)
        {
            List<T> result = new List<T>();
            object monitor = new object();

            bool countReached = false;

            ParallelOptions options = new ParallelOptions {CancellationToken = ct};

            Parallel.ForEach(
                items,
                options,
                () => new LinkedList<T>(),
                (item, state, local) =>
                {
                    ct.ThrowIfCancellationRequested();

                    foreach (T key in keys)
                    {
                        if (!item.Equals(key)) continue;
                        local.AddLast(item);
                        break;
                    }

                    if (result.Count > count)
                    {
                        Volatile.Write(ref countReached, true);
                        state.Break();
                    }

                    return local;
                },
                (toAccumulate) =>
                {
                    if (Volatile.Read(ref countReached)) return;

                    lock (monitor)
                    {
                        result.AddRange(toAccumulate);
                    }
                }
            );

            return result;
        }
    }
}