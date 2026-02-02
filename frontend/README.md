# ML CAPTCHA Frontend

React + TypeScript frontend for the ML-driven CAPTCHA platform.

## Features

- Sequence Memory CAPTCHA widget
- Behavior tracking (mouse movements, keystrokes)
- Responsive design
- TypeScript support

## Development

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Environment Variables

Create a `.env` file (see `.env.example`):

```env
VITE_API_URL=http://localhost:8080/api/v1
```

## Deployment

See [VERCEL_DEPLOYMENT.md](./VERCEL_DEPLOYMENT.md) for Vercel deployment instructions.

## Project Structure

```
src/
├── components/
│   └── captcha/
│       ├── CaptchaWidget.tsx
│       └── SequenceMemory.tsx
├── hooks/
│   └── useBehaviorTracking.ts
├── services/
│   ├── api.ts
│   └── captcha.ts
├── types/
│   └── captcha.ts
├── App.tsx
└── main.tsx
```

## API Integration

The frontend communicates with the backend API:

- `POST /api/v1/captcha/generate` - Generate challenge
- `POST /api/v1/captcha/verify` - Verify response

See [SEQUENCE_MEMORY_WIDGET.md](./SEQUENCE_MEMORY_WIDGET.md) for detailed API documentation.
