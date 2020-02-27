namespace TaskBasedAsynchronousPattern.TaskBasedAsynchronousPatternTests
{
    using System;
    using System.Threading.Tasks;
    using Xunit;

    class AsyncAwaitReturningContextTests
    {
        [Fact]
        public async void AwaitIOOperation_ExceptionIsCaught()
        {
            await AwaitedIOOperation();
        }

        [Fact]
        public async void NonAwaitedIOOperation_ExceptionIsNotCaught()
        {
            await NonAwaitedIOOperation();
        }

        private async Task AwaitedIOOperation()
        {
            try
            {
                await Task.Run(async () =>
                {
                    await Task.Delay(1000);
                    throw new InvalidProgramException("Invalid program");
                });
            }
            catch (Exception)
            {
                throw new Exception("This is caught.");
            }
        }

        private Task NonAwaitedIOOperation()
        {
            try
            {
                return Task.Run(async () =>
                {
                    await Task.Delay(1000);
                    throw new InvalidProgramException("Invalid program");
                });
            }
            catch (Exception)
            {
                throw new Exception("This should not be caught.");
            }
        }
    }
}
