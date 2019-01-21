using System;
using System.Threading;
using System.Threading.Tasks;

namespace TcpServer
{
    public static class TaskWithWaitCancellation
    {
        //Extension method on task class, its purpose its to be able to wait on the chosen task and on a 
        //completion source, this wait the task can be cancelled while it's waiting
        public static async Task<T> WithWaitCancellation<T>(
            this Task<T> task, CancellationToken cancellationToken)
        {
            // The task completion source. 
            var tcs = new TaskCompletionSource<bool>();

            // Register with the cancellation token.
            using (cancellationToken.Register(s => ((TaskCompletionSource<bool>) s).TrySetResult(true), tcs))
            {
                // If the task waited on is the cancellation token...
                if (task != await Task.WhenAny(task, tcs.Task))
                    throw new OperationCanceledException(cancellationToken);
            }

            // Wait for one or the other to complete.
            return await task;
        }
    }
}