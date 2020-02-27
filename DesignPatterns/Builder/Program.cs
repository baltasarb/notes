namespace Builder
{
    using System;
    using System.Text;

    class Program
    { 
        static void Main(string[] args)
        {
            Console.WriteLine("Hello World!");

            var stringBuilder = new StringBuilder();


            var htmlBuilder = new HtmlBuilder();

            var htmlPage = htmlBuilder
                            .P()
                            .Ul()
                            .Li()
                            .Li()
                            .P();

            Console.WriteLine(htmlPage.ToString());
        }
    }
}
