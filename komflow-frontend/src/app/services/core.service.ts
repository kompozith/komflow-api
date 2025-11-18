import { Injectable, signal } from '@angular/core';
import { AppSettings, defaults } from '../config';

@Injectable({
  providedIn: 'root',
})
export class CoreService {
  private readonly THEME_KEY = 'app_theme';
  private readonly LANGUAGE_KEY = 'app_language';
  private optionsSignal = signal<AppSettings>(this.loadSavedOptions());

  getOptions() {
    return this.optionsSignal();
  }

  setOptions(options: Partial<AppSettings>) {
    this.optionsSignal.update((current) => {
      const newOptions = {
        ...current,
        ...options,
      };
      // Save theme to localStorage when it changes
      if (options.theme) {
        this.saveTheme(options.theme);
      }
      // Save language to localStorage when it changes
      if (options.language) {
        this.saveLanguage(options.language);
      }
      return newOptions;
    });
  }

  private loadSavedOptions(): AppSettings {
    const savedTheme = this.getSavedTheme();
    const savedLanguage = this.getSavedLanguage();
    return {
      ...defaults,
      theme: savedTheme || defaults.theme,
      language: savedLanguage || defaults.language,
    };
  }

  private saveTheme(theme: string): void {
    try {
      localStorage.setItem(this.THEME_KEY, theme);
    } catch (error) {
      console.warn('Failed to save theme to localStorage:', error);
    }
  }

  private getSavedTheme(): string | null {
    try {
      return localStorage.getItem(this.THEME_KEY);
    } catch (error) {
      console.warn('Failed to load theme from localStorage:', error);
      return null;
    }
  }

  private saveLanguage(language: string): void {
    try {
      localStorage.setItem(this.LANGUAGE_KEY, language);
    } catch (error) {
      console.warn('Failed to save language to localStorage:', error);
    }
  }

  private getSavedLanguage(): string | null {
    try {
      return localStorage.getItem(this.LANGUAGE_KEY);
    } catch (error) {
      console.warn('Failed to load language from localStorage:', error);
      return null;
    }
  }

  setLanguage(lang: string) {
    this.setOptions({ language: lang });
  }

  getLanguage() {
    return this.getOptions().language;
  }
}
