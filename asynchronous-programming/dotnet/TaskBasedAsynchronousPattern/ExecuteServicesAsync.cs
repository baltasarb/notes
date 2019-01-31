using System.Threading.Tasks;

namespace TaskBasedAsynchronousPattern
{
    public class ExecuteServicesAsync
    {
        private class Response { }

        private class Request { }

        private class Session { }

        private class UserID { }

        private interface ITAPServices
        {
            Task<Session> LoginAsync(UserID uid);
            Task<Response> ExecServiceAsync(Session session, Request request);
            Task LogoutAsync(Session session);
        }

        private static async Task<Response[]> ExecServicesAsync(ITAPServices svc, UserID uid, Request[] requests)
        {
            Session session = await svc.LoginAsync(uid);

            try
            {
                var responseTasks = new Task<Response>[requests.Length];

                for (int i = 0; i < requests.Length; i++)
                    responseTasks[i] = svc.ExecServiceAsync(session, requests[i]);

                return await Task.WhenAll(responseTasks);
            }
            finally
            {
                try
                {
                    await svc.LogoutAsync(session);
                }
                catch { }
            }
        }
    }
}