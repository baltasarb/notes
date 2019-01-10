using System;
using System.Threading.Tasks;

namespace TaskBasedAsynchronousPattern
{
    public class GetUserAvatarAsync
    {
        private interface IService
        {
            Task<int> FindIdAsync(String name, String birthdate);
            Task<Uri> ObtainAvatarUriAsync(int userId);
        }

        //this implementation blocks the method because it forces the result to be waited upon by calling Result property.
        //this neutralizes any asynchronicity the method may have initially have
        private static Task<Uri> GetUserAvatarAsyncIncorrect(IService svc, String name, String bDate)
        {
            return Task.Run(() =>
            {
                int userId = svc.FindIdAsync(name, bDate).Result;
                return svc.ObtainAvatarUriAsync(userId).Result;
            });
        }


        private static async Task<Uri> GetUserAvatarAsyncCorrect(IService svc, String name, String bDate)
        {
            int userId = await svc.FindIdAsync(name, bDate);
            return await svc.ObtainAvatarUriAsync(userId);
        }

    }
}