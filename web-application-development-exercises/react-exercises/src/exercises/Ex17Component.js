import React from 'react'

export class Ex17Component extends React.Component{

    constructor(props){
        super(props);
        this.state =  {
            url : this.props.url
        };
        //this.drawResult = this.drawResult.bind(this);
    }

    componentDidMount(){
        fetch(this.state.url)
            .then(response => this.setState({result : response}));
    }

    render(){
        let body;
        if(this.state.result)
            body = this.drawResult();
        else
            body = this.loading();

        return (
            <div>
                <h1>Exercise 17</h1>
                {body}                
            </div>
        )
    }

    loading(){
        return <p>Loading...</p>;
    }

    drawResult(){      
        return <p>Status: {this.state.result.status}</p>;
    }
}

/*

Example HAL response

{
    _links:{
        "self": /student-group
    }
    _embedded:
}






*/