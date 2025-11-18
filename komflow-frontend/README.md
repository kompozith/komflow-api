# Komflow Frontend

Komflow is a comprehensive communication and messaging platform built with Angular and Spring Boot. This frontend application provides a modern, responsive admin dashboard for managing contacts, campaigns, messages, and user interactions.

## Features

- **Contact Management**: Create, edit, and manage contact lists with tagging capabilities
- **Campaign Management**: Design and execute messaging campaigns across multiple channels
- **Message Templates**: Create and manage reusable message templates
- **Audit Logging**: Comprehensive audit trail for all system activities
- **User Management**: Role-based access control and user administration
- **File Management**: Upload and manage files for campaigns and messages
- **Real-time Notifications**: Toast notifications and real-time updates

## Technology Stack

- **Framework**: Angular 20
- **UI Library**: Angular Material
- **Styling**: SCSS with Bootstrap
- **Icons**: Tabler Icons
- **Charts**: ApexCharts
- **State Management**: RxJS
- **Build Tool**: Angular CLI

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn
- Angular CLI 20+

### Installation

1. Clone the repository
2. Navigate to the frontend directory:
   ```bash
   cd komflow-frontend
   ```
3. Install dependencies:
   ```bash
   npm install
   ```

### Development

Start the development server:
```bash
npm start
```

The application will be available at `http://localhost:4200`

### Build

Build for production:
```bash
npm run build
```

### Testing

Run unit tests:
```bash
npm test
```

## Configuration

Environment configurations are located in `src/environments/`. Update the API URLs and other settings as needed for your deployment.

## Backend Integration

This frontend connects to the Komflow Spring Boot backend API. Ensure the backend is running and accessible at the configured API URL.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request

## License

This project is proprietary software developed by Kompozith.
