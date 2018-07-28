module.exports = {
    statusForAll
}

function statusForAll(urls) {
    function request(url) {
        return fetch(url)
            .then(response => {
                const statusCode = response.status;
                if (!Number.isInteger(statusCode % 400) || !Number.isInteger(statusCode % 500) || !response)
                    return Promise.reject();
                return response.status;
            });
    }
    return Promise.all(urls.map(url => request(url)));
}
