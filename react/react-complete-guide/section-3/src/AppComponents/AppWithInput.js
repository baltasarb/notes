import React, {Component} from 'react';
import './App.css';
import Person from '../Person/PersonWithInput';

class App extends Component {
    state = {
        name: "Person 1.0", 
        age: 1
    };

    nameChangedHandler = (event) => {
        this.setState({
            name: event.target.value
        })
    };

    render(){
        return (
            <div className="App">
                <Person name= {this.state.name} age = {this.state.age} nameChange={this.nameChangedHandler}/>
            </div>
        )
    }
}

export default App;