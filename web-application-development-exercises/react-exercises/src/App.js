import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { Ex7Component } from './Ex7ComponentWD';

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">Welcome to React</h1>
        </header>
        <p className="App-intro">
          To get started, edit <code>src/App.js</code> and save to reload.
        </p>
        <Ex7Component url='http://api.github.com' timeInterval={1000} callback={(statusCode) => console.log('Callback Status Code : ' + statusCode)}><h1>TESTE</h1></Ex7Component>
      </div>
    );
  }
}

export default App;
