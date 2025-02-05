# TokTokClone Development Workflow

## Daily Development Cycle

### 1. Session Start
```
1. Read current time
2. Check project-checklist.md
3. Calculate deadline status
4. Review any pending tasks
5. Set session goals based on timeline
```

### 2. Development Process
```
For each feature:
1. Planning
   - Review requirements
   - Check dependencies
   - Estimate time needed
   - Break into subtasks

2. Implementation
   - Create/modify necessary files
   - Follow MVVM architecture
   - Keep code modular
   - Add documentation

3. Testing
   - Manual testing
   - Error checking
   - Performance review

4. Documentation
   - Update project-checklist.md
   - Document decisions
   - Update progress tracking
```

### 3. Code Management
```
File Structure:
/app
  /src
    /main
      /java/com/toktokclone
        /data         # Data layer (repositories, data sources)
        /domain      # Business logic (use cases, models)
        /ui          # Presentation layer (screens, viewmodels)
        /di          # Dependency injection
        /utils       # Utility classes
      /res          # Resources
    /test           # Unit tests
```

### 4. Feature Implementation Steps
```
1. Data Layer
   - Define models
   - Create repository interfaces
   - Implement data sources

2. Domain Layer
   - Define use cases
   - Implement business logic
   - Add error handling

3. UI Layer
   - Create composables
   - Implement ViewModels
   - Add state management
```

## Task Workflow

### Phase 1 (PoC) Priority Order
```
1. Project Setup
   - Dependencies
   - Firebase config
   - Basic navigation

2. Core Features
   a. Authentication
      - Google Sign-in
      - User session management
   
   b. Video Recording
      - Camera setup
      - Basic recording
   
   c. Video Upload
      - Storage setup
      - Upload flow
   
   d. Video Playback
      - Feed setup
      - Basic player
```

### Phase 2 (MVP) Priority Order
```
1. Video Enhancement
   - Editing features
   - Quality settings

2. User Experience
   - Profiles
   - Feed improvements

3. Performance
   - Caching
   - Preloading
```

### Phase 3 (Polish) Priority Order
```
1. UI/UX Refinement
   - Animations
   - Loading states

2. Performance Optimization
   - Memory management
   - Battery usage

3. Testing & Documentation
   - Edge cases
   - User flows
```

## Progress Tracking

### Status Updates
```
After each significant change:
1. Update project-checklist.md
2. Calculate new progress percentages
3. Document any blockers
4. Update timeline if needed
```

### Quality Checks
```
Before marking task complete:
1. Code Review
   - Architecture compliance
   - Code style
   - Documentation
   
2. Testing
   - Functionality
   - Edge cases
   - Performance
   
3. Documentation
   - Code comments
   - README updates
   - Decision records
```

## Communication Protocol

### Status Reporting
```
Each response should include:
1. Current time
2. Time to next deadline
3. Current phase status
4. Immediate next steps
```

### Problem Resolution
```
When encountering issues:
1. Identify problem scope
2. List potential solutions
3. Evaluate timeline impact
4. Recommend best approach
5. Document decision
```

## Timeline Management

### Deadline Tracking
```
For each task:
1. Estimate completion time
2. Track actual time spent
3. Adjust remaining estimates
4. Flag if falling behind
```

### Priority Adjustment
```
If behind schedule:
1. Identify critical path
2. Cut non-essential features
3. Simplify complex features
4. Document trade-offs
``` 