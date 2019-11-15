import React, {useState} from 'react';

const Person = (props) => {
    const [personAgeState, setAgeState] = useState({
        age:props.age
    });
    
    const randomizeAge = () => {
        const newAge = Math.floor(Math.random() * 30);
        setAgeState({age:newAge});
    };

    return <p onClick={randomizeAge}>{props.name} is {personAgeState.age} years old.</p>;
};

export default Person;