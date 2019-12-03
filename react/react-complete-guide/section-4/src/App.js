import React, { Component } from 'react';
import './App.css';
import Person from './components/Person'

class App extends Component {

  state = {
    persons: [
      {
        id: 1,
        name: 'Person 1'
      },
      {
        id: 2,
        name: 'Person 2'
      }
    ],
    showPersons: false
  };

  togglePersonsHandler = () => {
    this.setState({ showPersons: !this.state.showPersons })
  };

  deletePersonHandler = (personIndex) => {
    let newPersons = [...this.state.persons].filter((person, currentIndex) => personIndex !== currentIndex);
    this.setState({
      persons: newPersons
    });
  };

  changePersonNameHandler = (personId, event) => {
    console.log(personId)
    const persons = [...this.state.persons]

    persons.forEach(person => {
      if(person.id === personId){
        person.name = event.target.value;
      }
    });

    // const personIndex = persons.find(person => person.id === personId);
    // const person = persons[personIndex];
    // console.log(person)
    // person.name = event.target.value;

    this.setState({
      persons: persons
    });
  };

  renderPersons() {
    return (
      <div>
        {
          this.state
            .persons
            .map((person, index) => this.renderPerson(person.id, person.name, index))
        }
      </div>
    )
  }

  renderPerson(id, name, index) {
    return (
      <Person
        key={id}
        name={name}
        deletePersonHandler={() => this.deletePersonHandler(index)}
        change={this.changePersonNameHandler.bind(this, id)}
      />
    );
  }

  render() {
    return (
      <div className="App">
        <h1>Section 4</h1>
        <button onClick={this.togglePersonsHandler}>Toggle</button>
        {this.state.showPersons ? this.renderPersons() : null}
      </div>
    )
  }
}

export default App;
