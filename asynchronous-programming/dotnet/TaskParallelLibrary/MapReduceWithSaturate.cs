using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace TaskParallelLibrary
{
    public class MapReduceWithSaturate
    {
        private class Result
        {
            public static Result SATURATED = null;
        }

        private class Data { }

        private static Result Map(Data d)
        {
            return null;
        }

        private static Result Reduce(Result r1, Result r2)
        {
            return null;
        }

        private static Result SequentialMapReduce(IEnumerable<Data> data)
        {
            Result r = null;
            foreach (var datum in data)
                if ((r = Reduce(r, Map(datum))) == Result.SATURATED)
                    break;
            return r;
        }

        private static Result ParallelMapReduce(IEnumerable<Data> data)
        {
            Result result = null;
            object monitor = new object();

            Parallel.ForEach(
                data,
                () => (Result) null,
                (datum, state, local) =>
                {
                    if ((local = Reduce(local, Map(datum))) == Result.SATURATED)
                    {
                        state.Stop();
                    }

                    return local;
                },
                (res) =>
                {
                    Result currentResult = Volatile.Read(ref result);
                    if (currentResult == Result.SATURATED)
                    {
                        return;
                    }

                    lock (monitor)
                    {
                        if (result != Result.SATURATED)
                            result = Reduce(result, res);
                    }
                });

            return result;
        }
    }
}