import { createContext, useState } from 'react';

export const FormatContext = createContext();

export const FormatProvider = ({ children }) => {
  const [selectedFormat, setSelectedFormat] = useState('ODI');
  return (
    <FormatContext.Provider value={{ selectedFormat, setSelectedFormat }}>
      {children}
    </FormatContext.Provider>
  );
};
