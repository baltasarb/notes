using System;
using System.Linq;
using System.Linq.Expressions;
using System.Threading.Tasks;

namespace TaskBasedAsynchronousPattern
{
    public class TAPExec
    {
        private interface ITAPServices
        {
            Task<Session> LoginAsync(UserId uid);
            Task<Response> ExecServiceAsync(Session session, Request request);
            Task LogoutAsync(Session session);
        }

        private static async Task<Response[]> ExecServicesAsync(ITAPServices svc, UserId uid, Request[] requests)
        {
            Task<Response>[] responseTasks = new Task<Response>[requests.Length];
            Session session;
            try
            {
                session = await svc.LoginAsync(uid);
            }
            catch
            {
                //throw illegal user exception
                throw;
            }

            for (int i = 0; i < requests.Length; i++)
                try
                {
                    responseTasks[i] = svc.ExecServiceAsync(session, requests[i]);
                }
                catch
                {
                    //on server too busy exception
                    responseTasks[i] = null;
                }

            await Task.WhenAll(responseTasks);

            await svc.LogoutAsync(session);

            return responseTasks.Select(task => task.Result).ToArray();
        }

        private class Session { }

        private class UserId { }

        private class Request { }

        private class Response { }
    }
}