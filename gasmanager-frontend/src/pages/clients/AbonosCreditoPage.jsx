import React from "react";
import {useParams} from "react-router-dom";
import Layout from "../../components/common/Layout.jsx";
import AbonosCredito from "../../components/clients/AbonosCredito.jsx";

const AbonosCreditoPage = () => {
    const {id} = useParams();
    return (
        <Layout>
            <AbonosCredito creditoId={id} />
        </Layout>
    );
}

export default AbonosCreditoPage;