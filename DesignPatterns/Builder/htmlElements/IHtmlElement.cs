namespace Builder.htmlElements
{
    using System;
    using System.Collections.Generic;
    using System.Text;

    interface IHtmlElement
    {
        public void Create();
        public IHtmlElement Finish();
    }
}
