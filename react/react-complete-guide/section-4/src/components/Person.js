import React, { Component } from 'react';

class Person extends Component {
    render() {
        return (
            <div>
                <h3>{this.props.name}</h3>
                <p onClick={this.props.deletePersonHandler}>click to delete {this.props.name}</p>
                <input type="text" placeholder="Input new person name" onChange={this.props.change} ></input>
            </div>
        )
    }
}

export default Person;