using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;


namespace TcpServer
{
    public class AsyncQueue<T>
    {
        //monitor used to lock list access
        private readonly object _monitor = new object();

        //if a receive happens first it will be kept here waiting to be finished by an arriving send or timeout/cancellation
        //used to deposit (empty if waiting) objects received in dequeue
        private readonly LinkedList<PendingMessage> _pendingConsumers;

        //if a send happens first the pending message (payload) will be kept here
        //used to deposit objects received in enqueue
        private readonly LinkedList<PendingMessage> _pendingProducers;

        //the object that will be kept in each list
        public class PendingMessage : TaskCompletionSource<T>
        {
            public T Payload { get; set; }
            public Timer Timer { get; set; }
            public CancellationTokenRegistration CancellationRegistration { get; set; }
        }

        public AsyncQueue()
        {
            _pendingConsumers = new LinkedList<PendingMessage>();
            _pendingProducers = new LinkedList<PendingMessage>();
        }

        //Adds a new payload to the pendingProducers queue if no one is currently waiting in the 
        //pending consumers queue.
        public void Enqueue(T payload)
        {
            lock (_monitor)
            {
                PendingMessage pendingMessage;

                ////fast path, check if someone is waiting to receive a payload
                if (_pendingConsumers.Any())
                {
                    pendingMessage = _pendingConsumers.First();
                    _pendingConsumers.RemoveFirst();

                    pendingMessage.CancellationRegistration.Dispose();
                    pendingMessage.Timer.Dispose();

                    pendingMessage.SetResult(payload);
                }

                else
                {
                    pendingMessage = new PendingMessage {Payload = payload};
                    _pendingProducers.AddLast(pendingMessage);
                }
            }
        }

        //Receives a new Payload from de pending producers queue if it is not empty or adds a new 
        //empty consumer to the pending consumers queue and waits (wait is made by utilizing an incomplete
        //task which will be complete only by a call to queue or timeout/cancellation occurs.
        public Task<T> Dequeue(TimeSpan timeout, CancellationTokenSource cancellationTokenSource)
        {
           lock (_monitor)
            {
                PendingMessage pendingMessage;

                if (_pendingProducers.Any())
                {
                    pendingMessage = _pendingProducers.First();
                    _pendingProducers.RemoveFirst();
                    pendingMessage.SetResult(pendingMessage.Payload);
                    return pendingMessage.Task;
                }

                pendingMessage = new PendingMessage();

                if (timeout.TotalMilliseconds <= 0)
                {
                    pendingMessage.SetException(new TimeoutException());
                    return pendingMessage.Task;
                }

                var node = _pendingConsumers.AddLast(pendingMessage);

                pendingMessage.Timer = new Timer(CancelDueToTimeout, node, timeout, new TimeSpan(-1));
                pendingMessage.CancellationRegistration =
                    cancellationTokenSource.Token.Register(CancelDueToCancellationToken, node);

                return pendingMessage.Task;
            }
        }

        //Callback to cancel the task received as parameter when a timeout occurs
        private void CancelDueToTimeout(object state)
        {
            var node = state as LinkedListNode<PendingMessage>;

            lock (_monitor)
            {
                if (node != null)
                {
                    node.Value.CancellationRegistration.Dispose();
                    _pendingConsumers.Remove(node);
                }

                node.Value.SetException(new TimeoutException());
            }
        }

        //Callback to cancel the task received as parameter when a task cancellation occurs
        private void CancelDueToCancellationToken(object state)
        {
            if (!(state is LinkedListNode<PendingMessage> node)) return;

            node.Value.Timer.Dispose();

            lock (_monitor)
            {
                _pendingConsumers.Remove(node);
            }

            node.Value.SetCanceled();
        }

        public LinkedList<PendingMessage> CancelAndGetQueueTasks()
        {
            lock (_monitor)
            {
                foreach (var pendingConsumer in _pendingConsumers)
                {
                    pendingConsumer.SetCanceled();
                }

                return _pendingConsumers;
            }
        }
    }
}