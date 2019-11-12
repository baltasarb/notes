//the reason react is imported is because iteration 1, behind the scenes, is equal to iteration 3.
import React from 'react';
import './App.css';

function App() {
  //Iteration 1
  // return (
  //   <div className="App">
  //     <h1>React Application</h1>
  //   </div>
  // );
  
  //Iteration 2
  //creates div with two lines of text inside
  //cecause h1 and React Application are interpreted as text
  //return React.createElement('div', null, 'h1', 'React Application'); 

  //Iteration 2
  //creates a div with an h1 tag inside, which haves React Application text inside
  //the css is not considered
  //return React.createElement('div', null, React.createElement('h1', null,  'React Application'));

  //Iteration 3
  //this code is the exact same code as iteration 1
  return React.createElement('div', {className: 'App'}, React.createElement('h1', null, 'React Application'));
}

export default App;
