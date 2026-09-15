import React from "react";

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = {error: null};
    }

    static getDerivedStateFromError(error) {
        return {error};
    }

    render() {
        if (this.state.error) {
            return (
                <div className="card" style={{margin: '2rem', padding: '1.5rem', borderColor: '#dc3545'}}>
                    <h3 className="text-danger">Error en la aplicación</h3>
                    <pre style={{whiteSpace: 'pre-wrap', color: '#dc3545', marginTop: '1rem'}}>
                        {String(this.state.error && (this.state.error.message || this.state.error))}
                    </pre>
                    <button className="btn btn-secondary" onClick={() => { this.setState({error: null}); window.location.href = '/dashboard'; }}>
                        Ir al Dashboard
                    </button>
                </div>
            );
        }
        return this.props.children;
    }
}

export default ErrorBoundary;