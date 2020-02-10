//the reason react is imported is because iteration 1, behind the scenes, is equal to iteration 3.
import React from 'react';
import './App.css';
import Person from '../Person/Person';
import PersonWithAge from '../Person/PersonWithRandomAge';
import PersonWithProps from '../Person/PersonWithProps';
import PersonWithPropsAndChildren from '../Person/PersonWithPropsAndChildren';

function AppFunction() {
  
  return (
    <div className="App">
      <h1>React Application</h1>
      <Person />
      <PersonWithAge />
      <PersonWithProps name="Person1" age="29"/>
      <PersonWithPropsAndChildren name="Person2" age="30">Component children.</PersonWithPropsAndChildren>
    </div>
  );
}

export default AppFunction;
