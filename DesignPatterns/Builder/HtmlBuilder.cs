namespace Builder
{
    using System;
    using System.Collections.Generic;
    using System.Text;

    class HtmlBuilder
    {
        private readonly HtmlElement root;
        private const int identation = 2;

        public HtmlBuilder(string rootType = "body")
        {
            this.root = new HtmlElement(rootType);
        }

        public void AddChild(string name, string)
        {

        }
    }
}
