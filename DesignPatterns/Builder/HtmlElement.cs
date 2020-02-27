namespace Builder
{
    using Builder.htmlElements;
    using System;
    using System.Collections.Generic;
    using System.Text;

    class HtmlElement : IHtmlElement
    {
        private readonly string openingTag;
        private readonly string closingTag;
        private List<HtmlElement> children;

        public HtmlElement(string type)
        {
            this.openingTag = $"<{type}>";
            this.closingTag = $"</{type}>";
            this.children = new List<HtmlElement>();
        }

        public void AddChild()
        {

        }

        public void Create()
        {
            throw new NotImplementedException();
        }

        public IHtmlElement Finish()
        {
            throw new NotImplementedException();
        }

        public override string ToString()
        {
            var stringBuilder = new StringBuilder();
            stringBuilder.Append(openingTag);

            foreach (var element in children){
                stringBuilder.Append(element.ToString());
            }

            stringBuilder.Append(closingTag);
            return stringBuilder.ToString();
        }
    }
}
