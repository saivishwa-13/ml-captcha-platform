import React, { useState } from 'react';
import CaptchaWidget from './components/captcha/CaptchaWidget';
import './App.css';

function App() {
  // Default API key for testing (any non-empty string works with backend)
  const [apiKey] = useState<string>(() => {
    const stored = localStorage.getItem('apiKey');
    return stored || 'test-api-key-123';
  });

  const handleSuccess = (token: string) => {
    console.log('CAPTCHA verified successfully:', token);
    alert('Verification successful! Token: ' + token);
  };

  const handleError = (error: string) => {
    console.error('CAPTCHA error:', error);
    alert('Error: ' + error);
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>ML-Driven CAPTCHA Platform</h1>
        <p style={{ fontSize: '14px', color: '#ccc', marginBottom: '20px' }}>
          Sequence Memory CAPTCHA Widget
        </p>
        <CaptchaWidget
          apiKey={apiKey}
          onSuccess={handleSuccess}
          onError={handleError}
          difficultyLevel={1}
        />
      </header>
    </div>
  );
}

export default App;
