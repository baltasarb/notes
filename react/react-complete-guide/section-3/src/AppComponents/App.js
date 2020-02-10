import React, { Component } from 'react';
import PersonWithProps from '../Person/PersonWithProps';

class App extends Component {
    state = {
        persons: [
            { name: 'Person1', age: 29 },
            { name: 'Person2', age: 30 },
            { name: 'Person3', age: 31 }
        ]
    }

    randomizePersonAges = () => {
        const generateRandomAge = () => {
            return Math.floor(Math.random() * 30);
        }

        this.setState({
            persons: [
                { name: 'Person1', age: generateRandomAge() },
                { name: 'Person2', age: generateRandomAge() },
                { name: 'Person3', age: generateRandomAge() }
            ]
        });
    }

    render() {
        return (
            <div className="App">
                <h1>React Application</h1>
                <button onClick={this.randomizePersonAges}>Randomize person ages</button>
                {createPersons(this.state.persons)}
            </div>
        )
    }
}

function createPersons(persons) {
    return persons.map(person => {
        return createPerson(person);
    })
}

function createPerson(person) {
    return <PersonWithProps key={person.name + person.age} name={person.name} age={person.age} />;
}

export default App;