import React from "react";
import {useParams} from "react-router-dom";
import Layout from "../../components/common/Layout.jsx";
import LiquidarNotas from "../../components/clients/LiquidarNotas.jsx";

const LiquidarNotasPage = () => {
    const {id} = useParams();
    return (
        <Layout>
            <LiquidarNotas creditoId={id} />
        </Layout>
    );
}

export default LiquidarNotasPage;