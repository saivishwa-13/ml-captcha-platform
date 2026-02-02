# Sequence Memory CAPTCHA Widget - Frontend Implementation

## Overview

A complete React frontend widget for Sequence Memory CAPTCHA that handles the full flow:
1. Calls `/captcha/generate` endpoint
2. Displays the sequence briefly
3. Hides the sequence after display duration
4. Collects user input
5. Calls `/captcha/verify` endpoint with behavior metrics

## Features

- ✅ Automatic challenge generation on mount
- ✅ Visual sequence display with countdown timer
- ✅ Support for numeric, symbolic, and alphanumeric sequences
- ✅ Real-time input validation
- ✅ Behavior tracking (mouse movements, keystrokes, timing)
- ✅ Error handling and retry logic
- ✅ Responsive UI with animations
- ✅ Loading and verification states

## Component Structure

### SequenceMemory Component

Main component that handles the complete CAPTCHA flow.

**Props:**
```typescript
interface SequenceMemoryProps {
  apiKey: string;              // API key for backend authentication
  difficultyLevel?: number;    // Difficulty level (1-5), defaults to 1
  onSuccess: (token: string) => void;  // Callback on successful verification
  onError: (error: string) => void;    // Callback on error
}
```

**States:**
- `loading`: Loading state
- `displaying`: Sequence is being shown
- `input`: Waiting for user input
- `verifying`: Verification in progress

### Behavior Tracking Hook

`useBehaviorTracking` hook tracks:
- Mouse movements (x, y, timestamp)
- Keystroke timings
- Total completion time

## Usage

### Basic Usage

```tsx
import SequenceMemory from './components/captcha/SequenceMemory';

function MyComponent() {
  const handleSuccess = (token: string) => {
    console.log('Verified! Token:', token);
    // Proceed with your application logic
  };

  const handleError = (error: string) => {
    console.error('CAPTCHA error:', error);
  };

  return (
    <SequenceMemory
      apiKey="your-api-key"
      difficultyLevel={1}
      onSuccess={handleSuccess}
      onError={handleError}
    />
  );
}
```

### Using with CaptchaWidget

```tsx
import CaptchaWidget from './components/captcha/CaptchaWidget';

function MyComponent() {
  return (
    <CaptchaWidget
      apiKey="your-api-key"
      difficultyLevel={1}
      onSuccess={(token) => console.log('Success:', token)}
      onError={(error) => console.error('Error:', error)}
    />
  );
}
```

## Sequence Types

### Numeric (Difficulty 1-2)
- Digits: 0-9
- Input: Number buttons

### Symbolic (Difficulty 3-4)
- Symbols: !, @, #, $, %, &, *, +, =, ?
- Input: Symbol buttons

### Alphanumeric (Difficulty 5)
- Mix of digits and letters A-Z
- Input: Alphanumeric buttons

## API Integration

### Generate Challenge

```typescript
const challenge = await captchaService.generateChallenge(
  apiKey,
  'sequence_memory',
  difficultyLevel
);

// Response:
{
  sessionToken: "uuid-token",
  challengeType: "sequence_memory",
  challengeData: {
    sequence: [3, 7, 2, 9],
    sequenceType: "numeric",
    displayDuration: 4000,
    length: 4
  },
  expiresAt: "2026-02-02T12:10:00"
}
```

### Verify Challenge

```typescript
const result = await captchaService.verifyChallenge(
  apiKey,
  sessionToken,
  { sequence: [3, 7, 2, 9] },
  behaviorMetrics
);

// Response:
{
  verified: true,
  confidence: 0.95,
  isHuman: true,
  token: "verification-token-uuid"
}
```

## Behavior Metrics

The widget automatically collects:

```typescript
{
  mouseMovements: [
    { x: 100, y: 200, timestamp: 150 },
    { x: 150, y: 250, timestamp: 300 },
    // ...
  ],
  keystrokeTimings: [
    { key: "3", timestamp: 4500 },
    { key: "7", timestamp: 4800 },
    // ...
  ],
  completionTime: 5200  // milliseconds
}
```

## Styling

The widget uses CSS classes for styling. Key classes:

- `.sequence-memory-challenge` - Main container
- `.sequence-display` - Sequence display section
- `.sequence-item` - Individual sequence items
- `.input-section` - User input section
- `.input-slot` - Input slots (empty/filled states)
- `.number-button`, `.symbol-button`, `.alphanumeric-button` - Input buttons
- `.action-button` - Action buttons (Submit, Reset, etc.)

Customize styles in `src/index.css`.

## Error Handling

The widget handles:
- Network errors (API failures)
- Invalid session tokens
- Expired sessions
- Verification failures
- Automatic retry on failure

## Development

### Running Locally

```bash
cd frontend
npm install
npm run dev
```

### Building

```bash
npm run build
```

### Testing

1. Ensure backend is running on `http://localhost:8080`
2. Open browser to `http://localhost:3000`
3. Widget will automatically load and display challenge

## Example Flow

1. **Component mounts** → Calls `/captcha/generate`
2. **Challenge received** → Displays sequence for `displayDuration` ms
3. **Sequence hidden** → Shows input interface
4. **User inputs sequence** → Validates length
5. **User submits** → Calls `/captcha/verify` with behavior metrics
6. **Verification success** → Calls `onSuccess` callback with token
7. **Verification failure** → Shows error, allows retry

## Notes

- Widget is self-contained and handles its own state
- Behavior tracking starts when challenge loads
- Sessions expire after 10 minutes (backend setting)
- Widget automatically retries on error
- Supports all sequence types (numeric, symbolic, alphanumeric)
- Responsive design works on mobile and desktop
