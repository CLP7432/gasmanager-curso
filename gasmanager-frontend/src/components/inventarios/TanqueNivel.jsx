import React from "react";
import LiquidFillGauge from "react-liquid-gauge";
import CardBase from "../../kernel/components/CardBase.jsx";

const paleta = (tipo) => {
    if (tipo === 'DIESEL') return {luz: '#7ec8ff', medio: '#2f82e0', oscuro: '#0b4fae'};
    if (tipo === 'PREMIUM') return {luz: '#ffb199', medio: '#e63946', oscuro: '#a4161a'};
    return {luz: '#9be98c', medio: '#4cc76e', oscuro: '#0f7a2f'};
};

const TanqueNivel = ({tanque}) => {
    const pct = tanque.capacidadLitros > 0
        ? (tanque.stockLitros / tanque.capacidadLitros) * 100
        : 0;
    const colores = paleta(tanque.tipoCombustible);
    const bajo = pct < 20;

    return (
        <CardBase titulo={tanque.nombre} style={{maxWidth: '300px', padding: '1rem', textAlign: 'center', border: bajo ? '2px solid #e74c3c' : undefined}}>
            <span className="badge bg-secondary mb-2">{tanque.tipoCombustible}</span>
            {bajo && <div><span className="badge bg-danger mb-2">¡Nivel bajo!</span></div>}
            <LiquidFillGauge
                width={220}
                height={220}
                value={Math.round(pct * 10) / 10}
                percent="%"
                textSize={0.7}
                textOffsetX={0}
                textOffsetY={0}
                riseAnimation
                riseAnimationTime={1200}
                waveAnimation
                waveAnimationTime={1800}
                waveFrequency={2}
                waveAmplitude={1}
                gradient
                gradientStops={[
                    {key: '0%',    stopColor: colores.luz,    stopOpacity: 1,    offset: '0%'},
                    {key: '50%',   stopColor: colores.medio,  stopOpacity: 0.75, offset: '50%'},
                    {key: '100%',  stopColor: colores.oscuro, stopOpacity: 0.5,  offset: '100%'}
                ]}
                circleStyle={{fill: colores.medio}}
                waveStyle={{fill: colores.medio}}
                textStyle={{fill: '#0f172a', fontFamily: 'Arial'}}
                waveTextStyle={{fill: '#ffffff', fontFamily: 'Arial'}}
                textRenderer={(props) => {
                    const valor = Math.round(props.value);
                    const radius = Math.min(props.height / 2, props.width / 2);
                    const textPixels = (props.textSize * radius / 2);
                    return (
                        <tspan>
                            <tspan style={{fontSize: textPixels}}>{valor}</tspan>
                            <tspan style={{fontSize: textPixels * 0.6}}>{props.percent}</tspan>
                        </tspan>
                    );
                }}
            />
            <div className="mt-2">
                <strong>{Number(tanque.stockLitros).toLocaleString('es-MX')} L</strong>
                <small className="text-muted"> de {Number(tanque.capacidadLitros).toLocaleString('es-MX')} L</small>
            </div>
            {pct < 20 && <div className="text-danger small mt-1">⚠ Nivel bajo</div>}
        </CardBase>
    );
};

export default TanqueNivel;