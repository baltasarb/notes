using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace TaskBasedAsynchronousPattern
{
    public class MapReduce
    {
        private class T { }

        private class R { }

        private R Map(T t)
        {
            return null;
        }

        private R Reduce(R r1, R r2)
        {
            return null;
        }

        private R SequentialMapReduce(T[] elems, R initial)
        {
            for (int i = 0; i < elems.Length; ++i)
            {
                initial = Reduce(Map(elems[i]), initial);
            }

            return initial;
        }

        private Task<R> MapAsync(T t)
        {
            return null;
        }

        private Task<R> ReduceAsync(R r1, R r2)
        {
            return null;
        }

        private async Task<R> ParallelMapReduce(T[] elems, R initial)
        {
            List<Task<R>> mappedValues = new List<Task<R>>();
    
            foreach (var element in elems)
            {
                var mappedValue = MapAsync(element);
                mappedValues.Add(mappedValue);
            }

            foreach(var mappedValue in mappedValues)
            {
                initial = await ReduceAsync(await mappedValue, initial);
            }

            return initial;
        }
    }
}