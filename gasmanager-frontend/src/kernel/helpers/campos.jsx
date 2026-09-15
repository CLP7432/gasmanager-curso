export const campo = (name, label, tipo, extra = {}) => ({name, label, type: tipo, ...extra});

export const campoTexto = (name, label, extra = {}) => campo(name, label, 'text', extra);
export const campoEmail = (name, label, extra = {}) => campo(name, label, 'email', extra);
export const campoPassword = (name, label, extra = {}) => campo(name, label, 'password', extra);
export const campoNumero = (name, label, extra = {}) => campo(name, label, 'number', extra);
export const campoFecha = (name, label, extra = {}) => campo(name, label, 'date', extra);
export const campoSelect = (name, label, opciones, extra = {}) => campo(name, label, 'select', {opciones, ...extra});
export const campoTextarea = (name, label, extra = {}) => campo(name, label, 'textarea', extra);
export const campoCheckbox = (name, label, extra = {}) => campo(name, label, 'checkbox', extra);
export const campoCurrency = (name, label, extra = {}) => campo(name, label, 'currency', extra);

