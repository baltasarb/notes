import React, { useState } from 'react';
import PersonWithProps from '../Person/PersonWithProps';
import PersonWithClickEvent from '../Person/PersonWithClickEvent';
import PersonWithInput from '../Person/PersonWithInput';
import './App.css';

const App = () => {
    const [personsState, setPersonsState] = useState({
        persons: [
            { name: 'Person1', age: 29 },
            { name: 'Person2', age: 30 }
        ]
    });

    const randomizePersonAges = () => {
        const generateRandomAge = () => {
            return Math.floor(Math.random() * 30);
        }
    
        setPersonsState({
            persons: [
                { name: 'Person1', age: generateRandomAge() },
                { name: 'Person2', age: generateRandomAge() },
                { name: 'Person3', age: generateRandomAge() }
            ]
        });
    };

    const randomizePersonAge = (age) => {
        return Math.floor(Math.random() * age);
    };

    return (
        <div className="App">
            <h1>React Application</h1>
            <button onClick={randomizePersonAges}>Randomize ages</button>
            {createPersons(personsState.persons)}
            <PersonWithClickEvent randomizeAge={randomizePersonAge} name="Person4" age="32"/>
            <PersonWithInput />
        </div>
    )
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





