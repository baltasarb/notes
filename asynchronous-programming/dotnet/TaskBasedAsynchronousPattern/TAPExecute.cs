using System.Threading.Tasks;

namespace TaskBasedAsynchronousPattern
{
    public class TAPExecute
    {
        private class A { }

        private class B { }

        private class C { }

        private class D { }

        //sync class
        private class Execute
        {
            private static D Run(IServices svc)
            {
                var a = svc.Oper1();
                return svc.Oper4(svc.Oper2(a),
                    svc.Oper3(a));
            }
        }

        //sync interface
        private interface IServices
        {
            A Oper1();
            B Oper2(A a);
            C Oper3(A a);
            D Oper4(B b, C c);
        }

        //async interface
        private interface ITAPServices
        {
            Task<A> Oper1Aync();
            Task<B> Oper2Async(A a);
            Task<C> Oper3Async(A a);
            Task<D> Oper4Async(B b, C c);
        }

        private static async Task<D> RunAsync(ITAPServices svc)
        {
            var a = await svc.Oper1Aync();

            var b = svc.Oper2Async(a);
            var c = svc.Oper3Async(a);

            return await svc.Oper4Async(await b, await c);
        }
    }
}