using System.Threading.Tasks;

namespace TaskBasedAsynchronousPattern
{
    public class Question4_1718i2
    {
        /*
         * given types and method
         */
        private class R { }

        private class T { }

        private static R Map(T t)
        {
            return null;
        }

        private static R Join(R r, R r2)
        {
            return null;
        }

        private static R[] MapJoin(T[] items)
        {
            var res = new R[items.Length / 2];
            for (int i = 0; i < res.Length; i++)
            {
                res[i] = Join(Map(items[2 * i]), Map(items[2 * i + 1]));
            }

            return res;
        }

        //assume async methods are implemented
        private static Task<R> MapAsync(T t)
        {
            return null;
        }

        private static Task<R> JoinAsync(R r, R r2)
        {
            return null;
        }

        private static async Task<R[]> MapJoinAsync(T[] items)
        {
            var res = new Task<R>[items.Length / 2];

            for (int i = 0; i < res.Length; i++)
            {
                res[i] = MapJoinTwoElementsAsync(items[2 * i], items[2 * i + 1]);
            }

            return await Task.WhenAll(res);
        }

        private static async Task<R> MapJoinTwoElementsAsync(T item1, T item2)
        {
            R[] result = await Task.WhenAll(MapAsync(item1), MapAsync(item2));
            return await JoinAsync(result[0], result[1]);
        }
    }
}