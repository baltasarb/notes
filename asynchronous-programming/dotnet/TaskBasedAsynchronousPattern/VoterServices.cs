using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace TaskBasedAsynchronousPattern
{
    public class VoterServices
    {
        //original class, interface and methods
        private class Question4
        {
            private interface IServices
            {
                Uri[] GetVoters(String question);
                bool GetAnswer(Uri voter, String question);
            }

            private static bool Query(IServices svc, String question)
            {
                Uri[] voters = svc.GetVoters(question);
                int agree, n;
                for (agree = 0, n = 1; n <= voters.Length; n++)
                {
                    agree += svc.GetAnswer(voters[n - 1], question) ? 1 : 0;
                    if (agree > (voters.Length / 2) || n - agree > (voters.Length / 2)) break;
                }

                return agree > voters.Length / 2;
            }

            //Tap services to use
            private interface ITapServices
            {
                Task<Uri[]> GetVotersAsync(String question);
                Task<bool> GetAnswerAsync(Uri voter, String question, CancellationToken cancellationToken);
            }

            private static async Task<bool> QueryAsync(ITapServices svc, String question)
            {
                Uri[] voters = await svc.GetVotersAsync(question);

                List<Task<bool>> tasks = new List<Task<bool>>();

                CancellationTokenSource source = new CancellationTokenSource();

                int agree = 0;
                int numberOfVotes = 0;

                for (int i = 0; i < voters.Length; i++)
                {
                    tasks.Add(svc.GetAnswerAsync(voters[i - 1], question, source.Token));
                }

                for (int i = 0; i < voters.Length; i++)
                {
                    var task = await Task.WhenAny(tasks);
                    tasks.Remove(task);
                    agree += task.Result ? 1 : 0;

                    if (agree > voters.Length / 2 || numberOfVotes - agree > voters.Length / 2)
                    {
                        break;
                    }
                }

                source.Cancel();

                await Task.WhenAll(tasks);

                return agree > voters.Length / 2;
            }
        }
    }
}