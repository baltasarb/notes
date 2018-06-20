import React from 'react'

//this component is the resolution of the exercise 7 of the Web Application Development Exercises
//https://github.com/baltasarb/notes/wiki/Exercises#web-application-development

export class Ex7Component extends React.Component{

    constructor(props){
        super(props)
        let statusCodes = [];
        this.state = {
            url : this.props.url,
            timeInterval : this.props.timeInterval,
            callback : this.props.callback,
            statusCodes,
        }
        this.request = this.request.bind(this)
    }

    componentDidMount(){
        setTimeout(this.request, this.state.timeInterval)
    }

    componentDidUpdate(){
        setTimeout(this.request, this.state.timeInterval)
    }

    request(){ 
        fetch(this.state.url)           
            .then(response => {
                if(!response || response.status !== 200)
                    this.state.callback(response.status)
                
                let codes = this.state.statusCodes
                codes.push(response.status)

                if(codes.length > 10)
                    codes = codes.slice(codes.length - 10, codes.length)
                    
                this.showCodes(codes)            
                this.setState({statusCodes: codes})
            })
            .catch(err => {
                this.state.callback('Erro _> ' + err)
            })
    }

    drawResponseStatusCodes(){           
        let i = 0 // for html keys
        console.log('LENGTH ' + this.state.statusCodes.length)
        return (<ul id="codeList">{this.state.statusCodes.map(code =><li key={code + i++}><b>{code}</b></li>)}</ul>)
    }

    showCodes(codes){        
        const length = codes.length        
        for(let i = length; length - i > 10; i--)                
            console.log(codes[i])
    }

    render(){
        return (
        <div> 
            <h1>Exercicio 7</h1>
            {this.drawResponseStatusCodes()}
        </div>
        )
    }
}