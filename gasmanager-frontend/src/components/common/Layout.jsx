
import React from 'react';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import AsistenteIA from './AsistenteIA';

const Layout = ({children}) => {

    return (
        <div className="app-layout">
            <Sidebar />
            <div className="main-content">
                <Navbar />
                {children}
                <AsistenteIA />
            </div>
        </div>
    )
}

export default Layout;