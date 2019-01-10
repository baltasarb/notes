using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace TaskParallelLibrary
{
    public class GetTopN
    {
        public T[] ParallelForGetTopN<T>(IEnumerable<T> items, int topSize) where T : IComparable<T>
        {
            BoundedOrderedQueue<T> boundedOrderedQueue = new BoundedOrderedQueue<T>(topSize);

            Parallel.ForEach(items,
                () => new BoundedOrderedQueue<T>(1),
                (item, state, queue) =>
                {
                    queue.Add(item);
                    return queue;
                }, queue =>
                {
                    lock (queue)
                    {
                        boundedOrderedQueue.Merge(queue);
                    }
                });

            return boundedOrderedQueue.ToArray();
        }
        
        //queue that sorts itself on insertion, used in the exercise. 
        public class BoundedOrderedQueue<T>
        {
            public BoundedOrderedQueue(int capacity) { }
            public void Add(T item) { }
            public void Merge(BoundedOrderedQueue<T> other) { }

            public T[] ToArray()
            {
                return null;
            }
        }
    }
}