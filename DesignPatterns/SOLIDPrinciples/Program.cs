namespace SOLIDPrinciples.SingleResponsibility
{
    using SOLIDPrinciples.OpenClosed;
    using System;

    class Program
    {
        public static void Main(string[] args)
        {
            TestSingleReposibility();
        }

        private static void TestSingleReposibility()
        {
            Journal journal = new Journal();

            int entry1Index = journal.AddEntry("Entry 1.");
            journal.AddEntry("Entry 2.");

            Console.WriteLine("Journal after two entries added:\n {0}.", journal);

            journal.RemoveEntry(entry1Index);

            Console.WriteLine("Journal after entry one removed:\n {0}.", journal);
        }
        
        private static void TestOpenClosed()
        {
            var orange = new Product { };
        }
    }
}
